// src/app/components/signup/signup.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, SignupData } from '../../services/auth.service';
import { MessageComponent } from "../shared/message.component";

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MessageComponent],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  signupForm: FormGroup;
  errorMessage= '';
  successMessage='';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.signupForm = this.fb.group({
      nom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      motdepasse: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.signupForm.valid) {
      const userData: SignupData = this.signupForm.value;
      this.authService.signup(userData).subscribe({
        next: () => {
          // Navigate to login after successful signup
          this.router.navigate(['/login']);
        },
        error: (error: Error) => {
          console.error('Signup error:', error);
          // Handle signup error (show message to user)
        }
      });
    }
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}