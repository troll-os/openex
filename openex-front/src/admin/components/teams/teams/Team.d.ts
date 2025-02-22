import type { TeamUpdateInput, Team } from '../../../../utils/api-types';
import { Option } from '../../../../utils/Option';

export type TeamInputForm = Omit<
TeamUpdateInput,
'team_organization' | 'team_tags'
> & {
  team_organization: Option | undefined;
  team_tags: Option[];
};
export type TeamStore = Omit<Team, 'team_organization' | 'team_tags'> & {
  team_organization: string | undefined;
  team_tags: string[] | undefined;
};
