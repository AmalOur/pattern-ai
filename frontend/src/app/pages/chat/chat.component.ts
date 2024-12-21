import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RepoDialogComponent } from '../../components/repo-dialog/repo-dialog.component';
import { SpaceService } from '../../services/space.service';
import { Discussion, Space } from '../../models/space.model';
import { RepositoryAnalysisRequest } from '../../models/repository.model';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RepoDialogComponent  
  ],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  spaces: Space[] = [];
  messages: Discussion[] = [];
  message = '';
  currentSpace: Space | null = null;
  isLoading = false;
  errorMessage = '';
  showRepoDialog = false;

  constructor(private spaceService: SpaceService) {}

  ngOnInit() {
    this.loadSpaces();
  }

  loadSpaces() {
    this.isLoading = true;
    this.spaceService.getUserSpaces().subscribe({
      next: (spaces) => {
        this.spaces = spaces;
        this.isLoading = false;
        
        // Auto-select first space if none selected
        if (spaces.length > 0 && !this.currentSpace) {
          this.onSpaceSelected(spaces[0]);
        }
      },
      error: (error) => {
        console.error('Error loading spaces:', error);
        this.errorMessage = 'Failed to load spaces';
        this.isLoading = false;
      }
    });
  }

  loadMessages() {
    if (!this.currentSpace) return;

    this.isLoading = true;
    this.spaceService.getChatHistory(this.currentSpace.id).subscribe({
      next: (messages) => {
        this.messages = messages;
        this.isLoading = false;
        this.scrollToBottom();
      },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.errorMessage = 'Failed to load message history';
        this.isLoading = false;
      }
    });
  }

  onSpaceSelected(space: Space) {
    this.currentSpace = space;
    this.loadMessages();
  }

  sendMessage() {
    if (!this.currentSpace) {
      this.errorMessage = 'Please select a space first';
      return;
    }

    const messageText = this.message.trim();
    if (!messageText) return;

    this.isLoading = true;
    this.errorMessage = '';
    
    // Store the message temporarily
    const tempMessage = this.message;
    this.message = ''; // Clear input immediately

    this.spaceService.sendMessage(this.currentSpace.id, messageText).subscribe({
      next: () => {
        this.isLoading = false;
        this.loadMessages(); // Reload messages to get the new one
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error sending message:', error);
        this.errorMessage = 'Failed to send message';
        this.message = tempMessage; // Restore message if failed
      }
    });
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      try {
        const element = this.messagesContainer.nativeElement;
        element.scrollTop = element.scrollHeight;
      } catch(err) {
        console.error('Error scrolling to bottom:', err);
      }
    });
  }

  // Helper method to check if message is from user
  isUserMessage(msg: Discussion): boolean {
    return msg.messageType === 'USER';
  }

  openRepoDialog() {
    this.showRepoDialog = true;
  }

  analyzeRepo(request: RepositoryAnalysisRequest) {
    if (!this.currentSpace) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.spaceService.analyzeRepository(this.currentSpace.id, request).subscribe({
      next: (response) => {
        console.log('Repository analysis completed:', response);
        this.showRepoDialog = false;
        this.isLoading = false;
        // Optionally show a success message
      },
      error: (error) => {
        console.error('Error analyzing repository:', error);
        this.errorMessage = 'Failed to analyze repository';
        this.isLoading = false;
      }
    });
  }
}