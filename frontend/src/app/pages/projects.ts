import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SiteService } from '../site.service';

@Component({
  selector: 'app-projects',
  imports: [RouterLink],
  templateUrl: './projects.html',
  styleUrl: './projects.scss'
})
export class Projects {
  readonly site = inject(SiteService);
  readonly filter = signal('all');

  readonly languages = computed(() => ['all', ...new Set([
    ...this.site.projects().map((p) => p.language ?? 'misc'),
    ...this.site.repos().map((r) => r.language ?? 'misc')
  ])]);

  readonly allCount = computed(() => new Set([
    ...this.site.projects().map((p) => p.slug.toLowerCase()),
    ...this.site.repos().map((r) => r.name.toLowerCase())
  ]).size);

  private readonly matches = (language: string) =>
    this.filter() === 'all' || language === this.filter();

  readonly visible = computed(() =>
    this.site.projects().filter((p) => this.matches(p.language ?? 'misc')));

  readonly visibleRepos = computed(() =>
    this.site.repos().filter((r) => this.matches(r.language ?? 'misc')));
}
