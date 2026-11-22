import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { GitHubRepo, Project, SiteContent, Update } from './models';

@Injectable({ providedIn: 'root' })
export class SiteService {
  private readonly http = inject(HttpClient);

  readonly content = signal<SiteContent | null>(null);
  readonly repos = signal<GitHubRepo[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly projects = computed(() => this.content()?.projects ?? []);
  readonly updates = computed<Update[]>(() =>
    [...(this.content()?.updates ?? [])].sort((a, b) => b.date.localeCompare(a.date)));

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http.get<SiteContent>('/api/site').subscribe({
      next: (data) => {
        this.content.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not reach the bench API. Try again in a moment.');
        this.loading.set(false);
      }
    });

    this.http.get<GitHubRepo[]>('/api/github/repos').subscribe({
      next: (data) => this.repos.set(data),
      error: () => this.repos.set([])
    });
  }

  hasFile(repo: GitHubRepo): boolean {
    return this.projects().some((p) => p.slug.toLowerCase() === repo.name.toLowerCase());
  }

  project(slug: string): Project | undefined {
    return this.projects().find((p) => p.slug === slug);
  }

  updatesFor(slug: string): Update[] {
    return this.updates().filter((u) => u.projectSlug === slug);
  }

  latest(project: Project): string {
    return project.versions?.[0]?.tag ?? '—';
  }

  date(value?: string | null): string {
    if (!value) return '';
    const parts = value.split('-');
    if (parts.length !== 3) return value;
    const months = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN',
      'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'];
    return `${parts[2]} ${months[Number(parts[1]) - 1] ?? ''} ${parts[0]}`;
  }
}
