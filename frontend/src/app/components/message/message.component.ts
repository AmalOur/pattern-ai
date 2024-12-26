// message.component.ts
import { Component, Input, SecurityContext } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Discussion } from '../../models/space.model';

@Component({
  selector: 'app-message',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './message.component.html',
  styleUrls: ['./message.component.css'],
})
export class MessageComponent {
  @Input() message!: Discussion;
  hasCodeBlock = false;
  isCopied = false;
  
  constructor(private sanitizer: DomSanitizer) {}

  get isUser(): boolean {
    return this.message.message.startsWith('User:');
  }

  get messageContainerClasses(): string {
    return this.isUser ? 'justify-end' : 'justify-start';
  }

  get messageBubbleClasses(): string {
    return `max-w-[85%] lg:max-w-[75%] rounded-lg px-4 py-3 shadow-sm ${
      this.isUser 
        ? 'bg-[#233349] text-white' 
        : 'bg-white border border-gray-100'
    }`;
  }

  get senderClasses(): string {
    return `flex items-center justify-between gap-2 mb-1 ${
      this.isUser 
        ? 'text-blue-200' 
        : 'text-gray-500'
    }`;
  }

  get contentClasses(): string {
    return this.isUser 
      ? 'prose-invert prose-pre:bg-blue-700 prose-code:text-blue-200' 
      : 'prose-gray prose-pre:bg-gray-800 prose-pre:text-gray-100';
  }

  get formattedMessage(): SafeHtml {
    let text = this.message.message.replace(/^(User:|Assistant:)\s*/, '');
    const formatted = this.formatMessageContent(text);
    return this.sanitizer.sanitize(SecurityContext.HTML, formatted) ?? '';
  }

  private formatMessageContent(text: string): string {
    if (!text) return '';
  
    // Check for code blocks
    this.hasCodeBlock = text.includes('```');
  
    // Format code blocks with syntax highlighting
    text = text.replace(/```(\w+)?\n([\s\S]*?)```/g, (_, lang, code) => {
      const language = lang || 'plaintext';
      return `<pre><code class="language-${language}">${this.escapeHtml(code.trim())}</code></pre>`;
    });
  
    // Format inline code
    text = text.replace(/`([^`]+)`/g, '<code>$1</code>');
  
    // Format bold text
    text = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  
    // Format italic text
    text = text.replace(/\*(.*?)\*/g, '<em>$1</em>');
  
    // Format lists
    text = text.replace(/^\s*[-*]\s+(.+)$/gm, '<li>$1</li>');
    text = text.replace(/(<li>.*?<\/li>\n?)+/g, '<ul>$&</ul>');
  
    // Format tables
    text = text.replace(
      /\n\|(.+?)\|(?:\n\|[-\s:]+\|)?\n((?:\|.+\|(?:\n|$))+)/g,
      (_, header, rows) => {
        const headerHtml = `<tr>${header
          .split('|')
          .map((cell: string) => `<th>${cell.trim()}</th>`)
          .join('')}</tr>`;
        const rowsHtml = rows
          .trim()
          .split('\n')
          .map((row: string) =>
            `<tr>${row
              .split('|')
              .map((cell: string) => `<td>${cell.trim()}</td>`)
              .join('')}</tr>`
          )
          .join('');
        return `<table>${headerHtml}${rowsHtml}</table>`;
      }
    );
  
    // Format line breaks
    text = text.replace(/\n\n/g, '</p><p>');
    text = text.replace(/\n/g, '<br>');
  
    // Wrap in paragraphs if not already wrapped
    if (!text.startsWith('<')) {
      text = `<p>${text}</p>`;
    }
  
    return text;
  }  

  private escapeHtml(text: string): string {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  async copyCode(code: string) {
    try {
      await navigator.clipboard.writeText(code);
      this.isCopied = true;
      setTimeout(() => this.isCopied = false, 2000); // Reset after 2s
    } catch (err) {
      console.error('Failed to copy code:', err);
    }
  }
}