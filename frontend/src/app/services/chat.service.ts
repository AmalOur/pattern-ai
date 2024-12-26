import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Message {
  id: number;
  content: string;
  timestamp: Date;
  sender: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://springboot-backend:8080/api/spaces';

  constructor(private http: HttpClient) {}

  getMessages(spaceId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/${spaceId}/messages`);
  }

  sendMessage(spaceId: number, content: string): Observable<Message> {
    return this.http.post<Message>(`${this.apiUrl}/${spaceId}/messages`, { content });
  }

  processRepository(spaceId: number, repoUrl: string, token: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${spaceId}/analyze`, { repoUrl, token });
  }
}
