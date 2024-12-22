import { Component, OnInit, ViewChild, ElementRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RepoDialogComponent } from '../../components/repo-dialog/repo-dialog.component';
import { SpaceService } from '../../services/space.service';
import { Discussion, Space } from '../../models/space.model';
import { RepositoryAnalysisRequest } from '../../models/repository.model';
import { SpaceDialogComponent } from '../../components/space-dialog/space-dialog.component';
import { HeaderComponent } from '../../components/header/header.component';
import { MessageComponent } from '../../components/message/message.component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderComponent,
    RepoDialogComponent,
    SpaceDialogComponent,
    MessageComponent
  ],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css'],
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
  showSpaceDialog = false;
  processedCode: any[] = [];
  isAtBottom = true;
  groupedSpaces: Map<string, Space[]> = new Map();
  isAnalyzing = false;

  constructor(
    private spaceService: SpaceService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadSpaces();
  }

  loadSpaces() {
    this.isLoading = true;
    this.spaceService.getUserSpaces().subscribe({
      next: (spaces) => {
        this.spaces = spaces;
        this.groupSpacesByDate();
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

  private groupSpacesByDate() {
    // Clear existing groups
    this.groupedSpaces = new Map();

    // Group spaces by date
    this.spaces.forEach(space => {
      const dateKey = this.spaceService.formatDate(space.createdAt);
      if (!this.groupedSpaces.has(dateKey)) {
        this.groupedSpaces.set(dateKey, []);
      }
      this.groupedSpaces.get(dateKey)?.push(space);
    });

    // Sort spaces within each group
    this.groupedSpaces.forEach((spaces, key) => {
      spaces.sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
    });
  }

  loadMessages() {
    if (!this.currentSpace) return;

    this.isLoading = true;
    this.spaceService.getChatHistory(this.currentSpace.id).subscribe({
      next: (messages) => {
        // Sort messages by creation date in ascending order
        this.messages = messages.sort((a, b) => 
          new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
        );
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
    this.loadProcessedCode(space.id);
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

  isUserMessage(msg: Discussion): boolean {
    return msg.message.startsWith('User:');
  }

  // Format message to remove prefix
  formatMessage(message: string): string {
    return message.replace(/^(User:|Assistant:)\s*/i, '').trim();
  }

  openRepoDialog() {
    this.showRepoDialog = true;
  }

  analyzeRepo(request: RepositoryAnalysisRequest) {
    if (!this.currentSpace) return;
  
    this.isLoading = true;
    this.isAnalyzing = true;
    this.errorMessage = '';
  
    this.spaceService.analyzeRepository(this.currentSpace.id, request).subscribe({
      next: (response) => {
        console.log('Repository analysis completed:', response);
        this.showRepoDialog = false;
        // Reload spaces and processed code
        this.loadSpaces();
        if (this.currentSpace) {
          this.loadProcessedCode(this.currentSpace.id);
        }
        this.isLoading = false;
        this.isAnalyzing = false;
      },
      error: (error) => {
        console.error('Error analyzing repository:', error);
        this.errorMessage = 'Failed to analyze repository';
        this.isLoading = false;
        this.isAnalyzing = false;
      }
    });
  }
  
  loadProcessedCode(spaceId: string) {
    this.spaceService.getProcessedCode(spaceId).subscribe({
      next: (code) => {
        this.processedCode = code;
      },
      error: (error) => {
        console.error('Error loading processed code:', error);
        this.errorMessage = 'Failed to load processed code';
      }
    });
  }

  createNewSpace() {
    this.showSpaceDialog = true;
  }

  handleSpaceCreation(spaceData: {name: string}) {
    this.isLoading = true;
    this.spaceService.createSpace(spaceData.name).subscribe({
      next: (newSpace) => {
        this.showSpaceDialog = false;
        this.spaces = [newSpace, ...this.spaces];
        this.groupSpacesByDate(); 
        this.onSpaceSelected(newSpace);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error creating space:', error);
        this.errorMessage = 'Failed to create space';
        this.isLoading = false;
      }
    });
  }

  deleteSpace() {
    if (!this.currentSpace) return;

    if (confirm('Are you sure you want to delete this space? This action cannot be undone.')) {
      this.isLoading = true;
      this.spaceService.deleteSpace(this.currentSpace.id).subscribe({
        next: () => {
          this.isLoading = false;
          this.spaces = this.spaces.filter(s => s.id !== this.currentSpace?.id);
          this.groupSpacesByDate(); // Regroup spaces after deletion
          this.currentSpace = null;
          this.messages = [];
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to delete space';
          console.error('Error deleting space:', error);
        }
      });
    }
  }

  deleteProcessedCode(collectionId: string) {
    if (!this.currentSpace) return;

    this.isLoading = true;
    this.spaceService.deleteProcessedCode(this.currentSpace.id, collectionId).subscribe({
      next: () => {
        this.processedCode = this.processedCode.filter(code => code.uuid !== collectionId);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error deleting processed code:', error);
        this.errorMessage = 'Failed to delete processed code';
        this.isLoading = false;
      }
    });
  }

  @HostListener('scroll', ['$event'])
  onScroll() {
    this.checkIfAtBottom();
  }

  checkIfAtBottom() {
    if (!this.messagesContainer) return;
    
    const container = this.messagesContainer.nativeElement;
    const threshold = 20; 
    const position = container.scrollHeight - container.scrollTop - container.clientHeight;
    this.isAtBottom = position < threshold;
  }

  scrollToBottom() {
    if (!this.messagesContainer) return;
    
    const container = this.messagesContainer.nativeElement;
    container.scrollTo({
      top: container.scrollHeight,
      behavior: 'smooth'
    });
  }
}