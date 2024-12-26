// src/app/components/repo-dialog/repo-dialog.component.ts
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RepositoryAnalysisRequest } from '../../models/repository.model';

@Component({
  selector: 'app-repo-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './repo-dialog.component.html',
  styleUrls: ['./repo-dialog.component.css'],
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