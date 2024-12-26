import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { User } from '../../services/auth.service';
import { Space } from '../../models/space.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  @Input() isSidebarOpen: boolean = true;
  @Input() currentSpace?: Space;
  @Input() processedCode: any[] = [];
  @Output() deleteSpaceEvent = new EventEmitter<void>();
  @Output() deleteProcessedCodeEvent = new EventEmitter<string>();

  isSpaceMenuOpen = false;
  isUserMenuOpen = false;
  user$;

  constructor(private authService: AuthService) {
    this.user$ = this.authService.user$;
  }

  toggleSpaceMenu() {
    this.isSpaceMenuOpen = !this.isSpaceMenuOpen;
    if (this.isSpaceMenuOpen) {
      this.isUserMenuOpen = false;
    }
  }

  closeSpaceMenu() {
    this.isSpaceMenuOpen = false;
  }

  toggleUserMenu() {
    this.isUserMenuOpen = !this.isUserMenuOpen;
    if (this.isUserMenuOpen) {
      this.isSpaceMenuOpen = false;
    }
  }

  extractRepoName(name: string): string {
    // If it's a GitHub URL, extract the repo name
    const parts = name.split('/');
    return parts[parts.length - 1] || name;
  }

  deleteProcessedCode(uuid: string) {
    if (confirm('Are you sure you want to remove this processed code?')) {
      this.deleteProcessedCodeEvent.emit(uuid);
      // Don't close the modal after deletion
    }
  }

  deleteSpace() {
    if (confirm('Are you sure you want to delete this space? This action cannot be undone.')) {
      this.deleteSpaceEvent.emit();
      this.closeSpaceMenu();
    }
  }

  logout() {
    this.authService.logout();
  }
}