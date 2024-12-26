import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Space } from '../../models/space.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  @Input() currentSpace: Space | null = null;
  @Input() spaces: Space[] = [];
  @Output() spaceSelected = new EventEmitter<Space>();
  @Output() spaceDeleted = new EventEmitter<string>();
  @Output() newSpace = new EventEmitter<void>();
  @Output() sidebarToggled = new EventEmitter<boolean>();

  isSidebarOpen = true;
  user$;

  constructor(private authService: AuthService) {
    this.user$ = this.authService.user$;
  }

  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
    this.sidebarToggled.emit(this.isSidebarOpen);
  }

  toggleSpaceMenu(space: Space) {
    space.isMenuOpen = !space.isMenuOpen;
  }

  onSpaceSelect(space: Space) {
    this.spaceSelected.emit(space);
  }
  
  createNewSpace() {
    this.newSpace.emit();
  }

  logout() {
    this.authService.logout();
  }
}