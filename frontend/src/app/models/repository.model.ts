// src/app/models/repository.model.ts
export interface RepositoryAnalysisRequest {
    repository_url: string;
    github_token: string;
}
  
export interface RepositoryAnalysisResponse {
    success: boolean;
    message?: string;
    error?: string;
    chunks_processed?: number;
    collection_name?: string;
}