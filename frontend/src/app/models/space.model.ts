export interface Space {
  id: string;
  name: string;
  createdAt: Date;
}

export interface Discussion {
  id: string;
  message: string;
  createdAt: Date;
  messageType: 'USER' | 'ASSISTANT';
}

export interface ChatResponse {
  message: Discussion;
  context?: {
    total_tokens?: number;
    completion_tokens?: number;
    prompt_tokens?: number;
  };
}

export interface ChatRequest {
  message: string;
  contextId?: string;
}