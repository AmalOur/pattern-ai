import psycopg2
from datetime import datetime
import json
import os
import uuid
import logging
from typing import List, Dict, Any, Optional

import requests
import numpy as np
from dotenv import load_dotenv
from flask import Flask, request, jsonify
from flask_cors import CORS
from sentence_transformers import SentenceTransformer
from langchain_community.vectorstores import PGVector
from langchain.embeddings.base import Embeddings
from langchain.text_splitter import RecursiveCharacterTextSplitter

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class CustomEmbeddings(Embeddings):
    def __init__(self):
        self.model = SentenceTransformer('all-MiniLM-L6-v2')
        
    def embed_documents(self, texts: List[str]) -> List[List[float]]:
        embeddings = self.model.encode(texts, normalize_embeddings=True)
        return embeddings.tolist()
    
    def embed_query(self, text: str) -> List[float]:
        embedding = self.model.encode([text], normalize_embeddings=True)
        return embedding[0].tolist()

# Initialize Flask app
app = Flask(__name__)
CORS(app, resources={
    r"/api/*": {
        "origins": ["http://localhost:8080"],
        "methods": ["GET", "POST", "OPTIONS"],
        "allow_headers": ["Content-Type", "Authorization"]
    }
})

# Database and embedding configuration
DB_CONFIG = {
    'connection_string': os.getenv('DATABASE_URL'),
    'embedding_function': CustomEmbeddings()
}

class GitHubService:
    """Service for handling GitHub operations"""
    
    @staticmethod
    def fetch_files(repo_url: str, github_token: str) -> Dict[str, Any]:
        """
        Fetch files from GitHub repository
        Returns both status and content/error message
        """
        if not repo_url or not github_token:
            return {
                "success": False,
                "error": "Repository URL and GitHub token are required"
            }

        try:
            if not repo_url.startswith('https://github.com/'):
                return {
                    "success": False,
                    "error": "Invalid GitHub URL"
                }

            owner, repo = repo_url.split('github.com/')[1].split('/')[:2]
            api_url = f'https://api.github.com/repos/{owner}/{repo}/contents'
            
            headers = {'Authorization': f'token {github_token}'}
            
            response = requests.get(api_url, headers=headers, timeout=30)
            response.raise_for_status()
            
            file_contents = []
            text_extensions = {'.md', '.txt', '.js', '.py', '.java', '.html', '.css'}
            
            def process_content(items, current_path=""):
                for item in items:
                    if item['type'] == 'file' and any(item['name'].endswith(ext) for ext in text_extensions):
                        file_response = requests.get(item['download_url'], headers=headers, timeout=30)
                        file_response.raise_for_status()
                        file_contents.append(
                            f"// File: {current_path}/{item['name']}\n{file_response.text}"
                        )
                    elif item['type'] == 'dir':
                        dir_response = requests.get(item['url'], headers=headers, timeout=30)
                        dir_response.raise_for_status()
                        process_content(dir_response.json(), f"{current_path}/{item['name']}")

            process_content(response.json())
            
            if not file_contents:
                return {
                    "success": False,
                    "error": "No processable files found in repository"
                }
                
            return {
                "success": True,
                "code_content": "\n\n".join(file_contents)
            }
            
        except requests.exceptions.RequestException as e:
            logger.error(f"GitHub API error: {e}")
            return {
                "success": False,
                "error": f"GitHub API error: {str(e)}"
            }

class VectorStoreService:
    """Service for managing vector store operations"""

    @staticmethod
    def ensure_collection_exists(collection_name: str, username: str) -> str:
        """Ensure collection exists and return its UUID"""
        conn = None
        try:
            conn = psycopg2.connect(DB_CONFIG['connection_string'])
            cur = conn.cursor()
            
            # Check if collection exists
            cur.execute(
                "SELECT uuid FROM langchain_pg_collection WHERE name = %s AND username = %s",
                (collection_name, username)
            )
            result = cur.fetchone()
            
            if result:
                collection_uuid = result[0]
            else:
                # Create new collection
                collection_uuid = str(uuid.uuid4())
                cur.execute(
                    """
                    INSERT INTO langchain_pg_collection (name, uuid, username, cmetadata)
                    VALUES (%s, %s, %s, %s)
                    """,
                    (collection_name, collection_uuid, username, json.dumps({'created_at': datetime.now().isoformat()}))
                )
            
            conn.commit()
            return collection_uuid
            
        except Exception as e:
            if conn:
                conn.rollback()
            raise e
        finally:
            if conn:
                cur.close()
                conn.close()

    @staticmethod
    def store_embeddings(collection_uuid: str, chunks: List[str], embeddings: List[List[float]], username: str):
        """Store embeddings in the database"""
        conn = None
        try:
            conn = psycopg2.connect(DB_CONFIG['connection_string'])
            cur = conn.cursor()
            
            for chunk, embedding in zip(chunks, embeddings):
                chunk_uuid = str(uuid.uuid4())
                cur.execute(
                    """
                    INSERT INTO langchain_pg_embedding 
                    (collection_id, embedding, document, uuid, username)
                    VALUES (%s, %s, %s, %s, %s)
                    """,
                    (collection_uuid, embedding, chunk, chunk_uuid, username)
                )
            
            conn.commit()
            
        except Exception as e:
            if conn:
                conn.rollback()
            raise e
        finally:
            if conn:
                cur.close()
                conn.close()

    @staticmethod
    def process_and_embed(code_content: str, collection_name: str, username: str) -> Dict[str, Any]:
        """Process and embed code content"""
        try:
            # Create text chunks
            splitter = RecursiveCharacterTextSplitter(
                chunk_size=1000,
                chunk_overlap=200,
                separators=["\n\n", "\n", " ", ""]
            )
            chunks = splitter.split_text(code_content)
            
            # Get embeddings
            embeddings_model = CustomEmbeddings()
            embeddings = embeddings_model.embed_documents(chunks)
            
            # Ensure collection exists
            collection_uuid = VectorStoreService.ensure_collection_exists(collection_name, username)
            
            # Store embeddings
            VectorStoreService.store_embeddings(collection_uuid, chunks, embeddings, username)
            
            return {
                "success": True,
                "chunks_processed": len(chunks),
                "collection_name": collection_name
            }
            
        except Exception as e:
            logger.error(f"Embedding error: {e}")
            return {
                "success": False,
                "error": f"Embedding error: {str(e)}"
            }


@app.route('/api/fetch_github_code', methods=['POST'])
def fetch_github_code():
    """Endpoint to fetch code from GitHub repository"""
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "No request data provided"}), 400

        repo_url = data.get('repository_url')
        github_token = data.get('github_token')

        result = GitHubService.fetch_files(repo_url, github_token)
        
        if not result['success']:
            return jsonify({"error": result['error']}), 400
            
        return jsonify(result), 200

    except Exception as e:
        logger.error(f"Error fetching code: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/api/embed_github_code', methods=['POST'])
def embed_github_code():
    """Endpoint to embed GitHub code"""
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "No request data provided"}), 400

        repo_url = data.get('repository_url')
        github_token = data.get('github_token')
        username = data.get('username', 'default_user')  # Get username from request
        collection_name = data.get('collection_name', f"repo_{hash(repo_url)}")

        # First fetch the code
        fetch_result = GitHubService.fetch_files(repo_url, github_token)
        if not fetch_result['success']:
            return jsonify({"error": fetch_result['error']}), 400

        # Then embed it with username
        embed_result = VectorStoreService.process_and_embed(
            fetch_result['code_content'],
            collection_name,
            username
        )
        
        if not embed_result['success']:
            return jsonify({"error": embed_result['error']}), 500

        return jsonify(embed_result), 200

    except Exception as e:
        logger.error(f"Error embedding code: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/health')
def health_check():
    """Health check endpoint"""
    try:
        # Try to connect to database
        PGVector(
            collection_name="health_check",
            connection_string=DB_CONFIG['connection_string'],
            embedding_function=DB_CONFIG['embedding_function']
        )
        return jsonify({"status": "healthy"}), 200
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        return jsonify({"status": "unhealthy", "error": str(e)}), 500

if __name__ == '__main__':
    # Required environment variables check
    required_vars = ['DATABASE_URL']
    missing_vars = [var for var in required_vars if not os.getenv(var)]
    if missing_vars:
        raise EnvironmentError(f"Missing required environment variables: {', '.join(missing_vars)}")
    
    port = int(os.getenv('PORT', 8000))
    debug = os.getenv('FLASK_DEBUG', 'False').lower() == 'true'
    
    logger.info(f"Starting server on port {port}")
    app.run(host='0.0.0.0', port=port, debug=debug)