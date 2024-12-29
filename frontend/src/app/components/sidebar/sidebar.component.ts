import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Space } from '../../models/space.model';
import { AuthService } from '../../services/auth.service';
import { ModelService, AIModel } from '../../services/model.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  @Input() currentSpace: Space | null = null;
  @Input() spaces: Space[] = [];
  @Output() spaceSelected = new EventEmitter<Space>();
  @Output() spaceDeleted = new EventEmitter<string>();
  @Output() newSpace = new EventEmitter<void>();
  @Output() sidebarToggled = new EventEmitter<boolean>();

  isSidebarOpen = true;
  user$;
  models: AIModel[] = [];
  currentModelId: string = '';
  isLoadingModels = false;

  constructor(
    private authService: AuthService,
    private modelService: ModelService
  ) {
    this.user$ = this.authService.user$;
  }

  ngOnInit() {
    this.loadModels();
  }

  private loadModels() {
    this.isLoadingModels = true;
    this.modelService.getAvailableModels().subscribe({
      next: (models) => {
        this.models = models;
        this.isLoadingModels = false;
        this.modelService.getCurrentModel().subscribe({
          next: (modelId) => this.currentModelId = modelId,
          error: (error) => console.error('Error getting current model:', error)
        });
      },
      error: (error) => {
        console.error('Error loading models:', error);
        this.isLoadingModels = false;
      }
    });
  }

  onModelChange(event: Event) {
    const select = event.target as HTMLSelectElement;
    const modelId = select.value;
    
    this.modelService.selectModel(modelId).subscribe({
      next: () => this.currentModelId = modelId,
      error: (error) => console.error('Error changing model:', error)
    });
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