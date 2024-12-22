// src/app/components/repo-dialog/repo-dialog.component.ts
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RepositoryAnalysisRequest } from '../../models/repository.model';

@Component({
  selector: 'app-repo-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 max-w-md w-full">
        <h2 class="text-xl font-semibold mb-4">Add GitHub Repository</h2>
        <form [formGroup]="repoForm" (ngSubmit)="onSubmit()">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Repository URL</label>
              <input
                type="text"
                formControlName="repoUrl"
                class="mt-1 block w-full rounded-md border-gray-300"
                placeholder="https://github.com/username/repo"
              >
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">GitHub Token</label>
              <input
                type="password"
                formControlName="token"
                class="mt-1 block w-full rounded-md border-gray-300"
              >
            </div>
          </div>
          <div class="mt-6 flex justify-end space-x-3">
            <button
              type="button"
              (click)="close()"
              class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md"
            >
              Cancel
            </button>
            <button
              type="submit"
              [disabled]="!repoForm.valid"
              class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md"
            >
              Analyze
            </button>
          </div>
        </form>
      </div>
    </div>
  `
})
export class RepoDialogComponent {
  @Input() isAnalyzing = false;
  @Output() submitted = new EventEmitter<RepositoryAnalysisRequest>();
  @Output() closed = new EventEmitter<void>();
  
  repoForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.repoForm = this.fb.group({
      repoUrl: ['', [Validators.required, Validators.pattern('https://github.com/.*')]],
      token: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.repoForm.valid) {
      const formValue = this.repoForm.value;
      // Map form values to the expected request format
      const request = {
        repositoryUrl: formValue.repoUrl,
        githubToken: formValue.token
      };
      console.log('Submitting repository analysis request:', { ...request, github_token: '****' });
      this.submitted.emit(request);
    }
  }

  close() {
    if (!this.isAnalyzing) {
      this.repoForm.reset();
      this.closed.emit();
    }
  }
}