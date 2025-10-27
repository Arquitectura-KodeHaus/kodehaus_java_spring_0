import { Injectable } from '@angular/core';

export interface User {
  id: number;
  name: string;
  roles: string[];
  permissions: string[]; // e.g., ['plaza:read', 'plaza:write']
  plazaId?: number;
  plan?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  // TODO: Replace this stub with real auth integration
  private user: User = {
    id: 1,
    name: 'Gerente de Plaza',
    roles: ['OWNER'],
    permissions: ['plaza:read', 'plaza:write'],
    plazaId: 1,
    plan: 'PRO'
  };

  getUser(): User {
    return this.user;
  }

  can(permission: string): boolean {
    return this.user.permissions.includes(permission);
  }

  get plazaId(): number | undefined {
    return this.user.plazaId;
  }
}
