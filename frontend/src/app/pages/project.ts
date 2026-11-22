import { Component, computed, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SiteService } from '../site.service';

@Component({
  selector: 'app-project-page',
  imports: [RouterLink],
  templateUrl: './project.html',
  styleUrl: './project.scss'
})
export class ProjectPage {
  readonly site = inject(SiteService);
  private readonly slug = inject(ActivatedRoute).snapshot.paramMap.get('slug') ?? '';

  readonly project = computed(() => this.site.project(this.slug));
  readonly entries = computed(() => this.site.updatesFor(this.slug));
}
