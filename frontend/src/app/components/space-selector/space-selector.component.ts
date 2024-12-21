import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Space } from '../../models/space.model';

@Component({
  selector: 'app-space-selector',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-selector w-full p-4">
      <div class="flex justify-between items-center mb-4">
        <button 
          (click)="createSpace.emit()"
          class="w-full p-4 bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          New Space
        </button>
      </div>
      
      <div class="space-y-2">
        <div 
          *ngFor="let space of spaces"
          (click)="onSpaceSelect(space)"
          class="flex items-center justify-between p-3 rounded-lg cursor-pointer transition-colors"
          [class.bg-blue-50]="space.id === selectedSpaceId"
          [class.hover:bg-gray-50]="space.id !== selectedSpaceId"
        >
          <div class="flex items-center space-x-3">
            <span class="text-gray-900">{{ space.name }}</span>
            <span class="text-sm text-gray-500">
              {{ space.createdAt | date:'MMM d, y' }}
            </span>
          </div>
          
          <button 
            *ngIf="space.id === selectedSpaceId"
            (click)="deleteSpace.emit(space.id); $event.stopPropagation()"
            class="text-red-600 hover:text-red-800"
          >
            Delete
          </button>
        </div>
      </div>
      
      <div *ngIf="spaces.length === 0" class="text-center py-8 text-gray-500">
        No spaces found. Create one to get started.
      </div>
    </div>
  `
})
export class SpaceSelectorComponent {
  @Input() spaces: Space[] = [];
  @Input() selectedSpaceId: string | null = null;
  @Output() spaceSelected = new EventEmitter<Space>();
  @Output() createSpace = new EventEmitter<void>();
  @Output() deleteSpace = new EventEmitter<string>();

  onSpaceSelect(space: Space) {
    this.spaceSelected.emit(space);
  }
}