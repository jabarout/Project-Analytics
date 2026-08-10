import { Injectable } from '@angular/core';
import { SavedExplorerView, ExplorerViewState } from '../models/explorer.model';

const STORAGE_KEY = 'pa.explorer.savedViews.v1';

/**
 * Local Saved Views store (M11A R5). Survives reload; M11B may later sync to backend.
 */
@Injectable({ providedIn: 'root' })
export class SavedViewsService {
  list(): SavedExplorerView[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return [];
      }
      const parsed = JSON.parse(raw) as SavedExplorerView[];
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  save(name: string, state: ExplorerViewState, existingId?: string): SavedExplorerView {
    const views = this.list();
    const now = new Date().toISOString();
    if (existingId) {
      const idx = views.findIndex((v) => v.id === existingId);
      if (idx >= 0) {
        const updated: SavedExplorerView = {
          ...views[idx],
          name,
          state,
        };
        views[idx] = updated;
        this.persist(views);
        return updated;
      }
    }
    const created: SavedExplorerView = {
      id: crypto.randomUUID(),
      name,
      state,
      createdAt: now,
    };
    views.push(created);
    this.persist(views);
    return created;
  }

  delete(id: string): void {
    this.persist(this.list().filter((v) => v.id !== id));
  }

  setDefault(id: string): void {
    const views = this.list().map((v) => ({
      ...v,
      isDefault: v.id === id,
    }));
    this.persist(views);
  }

  getDefault(): SavedExplorerView | null {
    return this.list().find((v) => v.isDefault) ?? null;
  }

  private persist(views: SavedExplorerView[]): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(views));
  }
}
