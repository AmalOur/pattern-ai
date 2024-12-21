import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RepoDialogComponent } from './repo-dialog.component';

describe('RepoDialogComponent', () => {
  let component: RepoDialogComponent;
  let fixture: ComponentFixture<RepoDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RepoDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RepoDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
