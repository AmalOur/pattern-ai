import { Component, Input, SecurityContext } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Discussion } from '../../models/space.model';

@Component({
  selector: 'app-message',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div [ngClass]="messageClasses">
      <div [class]="bubbleClasses">
        <div [class]="senderClasses">
          {{ isUser ? 'You' : 'Assistant' }}
          <span class="text-xs ml-2">{{ message.createdAt | date:'short' }}</span>
        </div>
        <div 
          class="prose prose-slate max-w-none"
          [innerHTML]="formattedMessage"
        ></div>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      margin-bottom: 1rem;
    }

    :host ::ng-deep {
      .prose pre {
        background-color: rgb(243, 244, 246);
        padding: 1rem;
        border-radius: 0.375rem;
        margin: 0.5rem 0;
        white-space: pre-wrap;
        word-wrap: break-word;
      }

      .prose code {
        font-family: ui-monospace, monospace;
        font-size: 0.875em;
        color: rgb(31, 41, 55);
        padding: 0.2em 0.4em;
        background-color: rgba(0, 0, 0, 0.05);
        border-radius: 3px;
      }

      .prose pre code {
        padding: 0;
        background-color: transparent;
        border-radius: 0;
      }
    }
  `]
})
export class MessageComponent {
  @Input() message!: Discussion;
  
  constructor(private sanitizer: DomSanitizer) {}

  get isUser(): boolean {
    return this.message.messageType === 'USER';
  }

  get messageClasses(): string {
    return `flex ${this.isUser ? 'justify-end' : 'justify-start'}`;
  }

  get bubbleClasses(): string {
    return `${
      this.isUser 
        ? 'bg-blue-600 text-white' 
        : 'bg-white shadow'
    } rounded-lg px-4 py-2 max-w-[80%]`;
  }

  get senderClasses(): string {
    return `text-sm ${
      this.isUser 
        ? 'text-blue-200' 
        : 'text-gray-500'
    } mb-1`;
  }

  get formattedMessage(): SafeHtml {
    const formatted = this.formatMessage(this.message.message);
    return this.sanitizer.sanitize(SecurityContext.HTML, formatted) ?? '';
  }

  private formatMessage(text: string): string {
    if (!text) return '';

    // Replace code blocks with proper HTML
    text = text.replace(/```([\s\S]*?)```/g, (match, code) => {
      return `<pre><code>${this.escapeHtml(code.trim())}</code></pre>`;
    });
    
    // Replace inline code with proper HTML
    text = text.replace(/`([^`]+)`/g, (match, code) => {
      return `<code>${this.escapeHtml(code)}</code>`;
    });
    
    // Convert line breaks while preserving paragraphs
    text = text.replace(/\n\n+/g, '</p><p>');
    text = text.replace(/\n/g, '<br>');
    
    // Wrap in paragraphs if not already
    if (!text.startsWith('<p>')) {
      text = '<p>' + text + '</p>';
    }
    
    return text;
  }

  private escapeHtml(text: string): string {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
}