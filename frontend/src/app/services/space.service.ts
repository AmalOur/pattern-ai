import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { Space, Discussion, ChatResponse } from '../models/space.model';
import { RepositoryAnalysisResponse, RepositoryAnalysisRequest } from '../models/repository.model';

@Injectable({
  providedIn: 'root'
})
export class SpaceService {
  private apiUrl = 'http://localhost:8080/api/spaces';

  constructor(private http: HttpClient) {}

  getUserSpaces(): Observable<Space[]> {
    return this.http.get<Space[]>(this.apiUrl).pipe(
      map(spaces => spaces.sort((a, b) => {
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
      }))
    );
  }

  // Add helper method to format date
  formatDate(date: Date): string {
    const now = new Date();
    const spaceDate = new Date(date);
    const diffTime = Math.abs(now.getTime() - spaceDate.getTime());
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 0) {
      return 'Today';
    } else if (diffDays === 1) {
      return 'Yesterday';
    } else if (diffDays < 7) {
      return `${diffDays} days ago`;
    } else {
      return spaceDate.toLocaleDateString();
    }
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

  sendMessage(spaceId: string, query: string): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(
      `${this.apiUrl}/${spaceId}/chat/design-patterns`,
      { query }
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

  getProcessedCode(spaceId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${spaceId}/collections`);
  }

  deleteProcessedCode(spaceId: string, collectionId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${spaceId}/collections/${collectionId}`);
  }

}