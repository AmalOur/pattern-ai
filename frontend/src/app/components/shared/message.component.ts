import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { trigger, style, animate, transition } from '@angular/animations';

@Component({
  selector: 'app-message',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="message" 
         class="message" 
         [class.error]="type === 'error'"
         [class.success]="type === 'success'"
         [@fadeInOut]>
      <span class="message-text">{{ message }}</span>
    </div>
  `,
  styles: [`
    .message {
      padding: 12px;
      border-radius: 6px;
      margin-bottom: 20px;
      text-align: center;
      font-size: 0.9rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .error {
      background-color: #fee2e2;
      border: 1px solid #fecaca;
      color: #dc2626;
    }
    
    .success {
      background-color: #dcfce7;
      border: 1px solid #bbf7d0;
      color: #16a34a;
    }

    .message-text {
      display: inline-block;
    }
  `],
  animations: [
    trigger('fadeInOut', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(-10px)' }),
        animate('300ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ opacity: 0, transform: 'translateY(-10px)' }))
      ])
    ])
  ]
})
export class MessageComponent {
  @Input() message: string = '';
  @Input() type: 'error' | 'success' = 'error';
}