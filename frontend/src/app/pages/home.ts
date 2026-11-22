import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SiteService } from '../site.service';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home {
  readonly site = inject(SiteService);

  readonly featured = computed(() => this.site.projects()[0]);
  readonly projectCount = computed(() => String(this.site.projects().length).padStart(2, '0'));
  readonly tags = computed(() =>
    [...new Set(this.site.projects().flatMap((p) => p.tags ?? []))].slice(0, 8));
  readonly recent = computed(() => this.site.updates().slice(0, 4));
}
