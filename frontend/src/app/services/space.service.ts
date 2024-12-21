import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Space, Discussion } from '../models/space.model';
import { RepositoryAnalysisResponse, RepositoryAnalysisRequest } from '../models/repository.model';

@Injectable({
  providedIn: 'root'
})
export class SpaceService {
  private apiUrl = 'http://localhost:8080/api/spaces';

  constructor(private http: HttpClient) {}

  getUserSpaces(): Observable<Space[]> {
    return this.http.get<Space[]>(this.apiUrl);
  }

  createSpace(name: string): Observable<Space> {
    return this.http.post<Space>(this.apiUrl, { name });
  }

  deleteSpace(spaceId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${spaceId}`);
  }

  getChatHistory(spaceId: string): Observable<Discussion[]> {
    return this.http.get<Discussion[]>(`${this.apiUrl}/${spaceId}/chat/history`);
  }

  sendMessage(spaceId: string, message: string): Observable<any> {
    return this.http.post<any>(
      `${this.apiUrl}/${spaceId}/chat/send`,
      { message }
    );
  }

  analyzeRepository(spaceId: string, data: RepositoryAnalysisRequest): Observable<RepositoryAnalysisResponse> {
    return this.http.post<RepositoryAnalysisResponse>(
      `${this.apiUrl}/${spaceId}/analyze`,
      data
    );
  }

  getSpaceCollections(spaceId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${spaceId}/collections`);
  }

  deleteCollection(spaceId: string, collectionId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${spaceId}/collections/${collectionId}`);
  }

  getSimilarCode(spaceId: string, collectionId: string, query: string): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.apiUrl}/${spaceId}/collections/${collectionId}/similar`,
      { params: { codeQuery: query } }
    );
  }
}