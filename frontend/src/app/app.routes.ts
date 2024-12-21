// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'chat',
    pathMatch: 'full'
  },
  {
    path: 'signup',
    loadComponent: () => import('./components/signup/signup.component')
      .then(m => m.SignupComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component')
      .then(m => m.LoginComponent)
  },
  {
    path: 'chat',
    loadComponent: () => import('./pages/chat/chat.component')
      .then(m => m.ChatComponent),
    canActivate: [AuthGuard]
  }
];