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

  readonly languages = computed(() =>
    ['all', ...new Set(this.site.projects().map((p) => p.language ?? 'misc'))]);

  readonly visible = computed(() => {
    const f = this.filter();
    return f === 'all'
      ? this.site.projects()
      : this.site.projects().filter((p) => (p.language ?? 'misc') === f);
  });
}
