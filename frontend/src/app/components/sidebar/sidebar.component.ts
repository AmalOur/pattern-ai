import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Space } from '../../models/space.model';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  @Input() spaces: Space[] = [];
  @Output() spaceSelected = new EventEmitter<Space>();
  @Output() spaceDeleted = new EventEmitter<string>();
  @Output() newSpace = new EventEmitter<void>();

  toggleSpaceMenu(space: Space) {
    space.isMenuOpen = !space.isMenuOpen;
  }

  onSpaceSelect(space: Space) {
    this.spaceSelected.emit(space);
  }

  onSpaceDelete(spaceId: string) {
    this.spaceDeleted.emit(spaceId);
  }

  createNewSpace() {
    this.newSpace.emit();
  }

  viewCode(space: Space) {
    // Implement code viewing logic
    console.log('Viewing code for space:', space.id);
  }
}