export interface Link { label: string; url: string; }

export interface Block {
  type: 'text' | 'features' | 'quote' | 'install' | 'links';
  title?: string;
  body?: string;
  items?: string[];
  links?: Link[];
}

export interface Landing {
  eyebrow?: string;
  headline?: string;
  deck?: string;
  ctaLabel?: string;
  ctaUrl?: string;
  blocks?: Block[];
}

export interface Version {
  tag: string;
  date: string;
  title?: string;
  notes?: string;
  changelogUrl?: string;
  downloadUrl?: string;
  current?: boolean;
}

export interface Update {
  date: string;
  title: string;
  body: string;
  kind: string;
  projectSlug?: string | null;
}

export interface Project {
  slug: string;
  name: string;
  tagline?: string;
  description?: string;
  status?: string;
  language?: string;
  repoUrl?: string;
  wikiUrl?: string;
  demoUrl?: string;
  tags?: string[];
  accent?: string;
  landing?: Landing;
  versions?: Version[];
  updates?: Update[];
}

export interface Profile {
  handle: string;
  name?: string;
  role?: string;
  location?: string;
  since?: string;
  bio?: string;
  links?: Link[];
}

export interface SiteContent {
  profile: Profile;
  projects: Project[];
  updates: Update[];
}
