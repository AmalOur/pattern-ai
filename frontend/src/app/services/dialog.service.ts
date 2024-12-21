// services/dialog.service.ts
import { Injectable, ComponentRef, createComponent, ApplicationRef, Type, Injector, EmbeddedViewRef } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DialogService {
  private dialogComponentRef: ComponentRef<any> | null = null;

  constructor(
    private appRef: ApplicationRef,
    private injector: Injector
  ) {}

  open(component: Type<any>, data?: any) {
    this.closeDialog();

    // Create component
    this.dialogComponentRef = createComponent(component, {
      environmentInjector: this.appRef.injector,
      elementInjector: this.injector
    });

    // Set data if provided
    if (data) {
      Object.assign(this.dialogComponentRef.instance, data);
    }

    // Attach to DOM
    const domElem = (this.dialogComponentRef.hostView as EmbeddedViewRef<any>).rootNodes[0];
    document.body.appendChild(domElem);

    // Attach to app
    this.appRef.attachView(this.dialogComponentRef.hostView);

    return this.dialogComponentRef;
  }

  closeDialog() {
    if (this.dialogComponentRef) {
      this.appRef.detachView(this.dialogComponentRef.hostView);
      this.dialogComponentRef.destroy();
      this.dialogComponentRef = null;
    }
  }
}