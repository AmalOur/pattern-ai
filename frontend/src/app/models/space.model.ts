export interface Space {
  id: string;
  name: string;
  createdAt: Date;
}

export interface Discussion {
  id: string;
  message: string;
  createdAt: Date;
  messageType: string;
  space?: Space;
}

export interface ChatResponse {
  userMessage: Discussion;
  assistantMessage: Discussion;
  full_response: string;
  codeFound: boolean;
  usage?: {
    prompt_tokens: number;
    completion_tokens: number;
    total_tokens: number;
  };
}

export interface ChatRequest {
  message: string;
  contextId?: string;
}