import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';

export interface AIModel {
  id: string;
  name: string;
  contextLength: number;
}

@Injectable({
  providedIn: 'root'
})
export class ModelService {
  private apiUrl = 'http://localhost:8080/api/models';
  private currentModelSubject = new BehaviorSubject<string>('');
  currentModel$ = this.currentModelSubject.asObservable();

  constructor(private http: HttpClient) {
    this.getCurrentModel().subscribe({
      next: modelId => this.currentModelSubject.next(modelId),
      error: () => console.error('Error getting current model')
    });
  }

  getAvailableModels(): Observable<AIModel[]> {
    return this.http.get<AIModel[]>(this.apiUrl);
  }

  getCurrentModel(): Observable<string> {
    return this.http.get<string>(`${this.apiUrl}/current`);
  }

  selectModel(modelId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/select`, { modelId });
  }
}