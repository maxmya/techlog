import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink],
  template: `
    <section class="wrap page">
      <span class="kicker oxide">// 404 — NOT ON THE SHELF</span>
      <h1 class="display page-title">WRONG TURN<span class="oxide">.</span></h1>
      <p class="page-intro">That page isn't here. The bench is still where you left it.</p>
      <div class="actions"><a class="btn solid" routerLink="/">BACK TO THE BENCH <span>↗</span></a></div>
    </section>
  `,
  styles: [`.actions { margin-top: 32px; }`]
})
export class NotFound {}
