// src/app/pages/login/login.component.ts
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { MessageComponent } from "../shared/message.component";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MessageComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      motdepasse: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value).subscribe({
        next: (response) => {
          if (response.token) {
            this.successMessage = 'Login successful!';
              this.router.navigate(['/chat'], {
                replaceUrl: true // This will replace the current URL in history
              }).then(navigated => {
                if (!navigated) {
                  console.error('Navigation to /chat failed');
                  this.errorMessage = 'Navigation failed. Please try again.';
                }
              }).catch(err => {
                console.error('Navigation error:', err);
                this.errorMessage = 'Navigation error occurred.';
              });
          } else {
            this.errorMessage = response.message || 'Login failed';
          }
        },
        error: (error) => {
          if (error.status === 401) {
            this.errorMessage = 'Invalid email or password';
          } else {
            this.errorMessage = error.error?.message || 'An error occurred during login';
          }
          console.error('Login error:', error);
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
}

  goToSignup() {
    this.router.navigate(['/signup']);
  }
}