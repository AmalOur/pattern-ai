// src/app/models/repository.model.ts
export interface RepositoryAnalysisRequest {
    repositoryUrl: string;
    githubToken: string;
}
  
export interface RepositoryAnalysisResponse {
    success: boolean;
    message?: string;
    error?: string;
    chunks_processed?: number;
    collection_name?: string;
}