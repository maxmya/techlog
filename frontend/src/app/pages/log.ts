import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SiteService } from '../site.service';

@Component({
  selector: 'app-log',
  imports: [RouterLink],
  templateUrl: './log.html',
  styleUrl: './log.scss'
})
export class Log {
  readonly site = inject(SiteService);
  readonly filter = signal('all');

  readonly kinds = computed(() =>
    ['all', ...new Set(this.site.updates().map((u) => u.kind))]);

  readonly visible = computed(() => {
    const f = this.filter();
    return f === 'all' ? this.site.updates() : this.site.updates().filter((u) => u.kind === f);
  });
}
