import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink],
  template: `
    <section class="wrap page">
      <div class="head-top"><span>FIELD NOTES</span><span>MISSING PAGE</span></div>
      <span class="kicker oxide">// 404 — NOT ON THE SHELF</span>
      <h1 class="page-title">Wrong turn<span class="oxide">.</span></h1>
      <p class="page-intro">That page isn't here. The bench is still where you left it.</p>
      <div class="actions"><a class="btn solid" routerLink="/">BACK TO THE BENCH <span>↗</span></a></div>
    </section>
  `,
  styles: [`
    .head-top { display: flex; justify-content: space-between; border-bottom: 1px solid var(--ink); padding-bottom: 8px; margin-bottom: 34px; font-size: .6rem; letter-spacing: .14em; text-transform: uppercase; color: var(--muted); }
    .page-title { font-family: var(--display); font-size: clamp(4rem, 10vw, 8rem); font-weight: 500; letter-spacing: -.045em; margin: 14px 0 16px; }
    .actions { margin-top: 32px; }
  `]
})
export class NotFound {}
