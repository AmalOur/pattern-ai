import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SpaceDialogComponent } from './space-dialog.component';

describe('SpaceDialogComponent', () => {
  let component: SpaceDialogComponent;
  let fixture: ComponentFixture<SpaceDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SpaceDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SpaceDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
