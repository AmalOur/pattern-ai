import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-space-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './space-dialog.component.html',
  styleUrls: ['./space-dialog.component.css']
})
export class SpaceDialogComponent {
  @Output() submitted = new EventEmitter<{name: string}>();
  @Output() closed = new EventEmitter<void>();

  spaceForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.spaceForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]]
    });
  }

  onSubmit() {
    if (this.spaceForm.valid) {
      this.submitted.emit(this.spaceForm.value);
    }
  }

  onClose() {
    this.closed.emit();
  }
}