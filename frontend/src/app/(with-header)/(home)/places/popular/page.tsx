export const dynamic = 'force-dynamic';

import { authenticateUser } from '@/app/_components/mypage/hooks/authenticateUser';
import { myInfoSchema } from '@/types/response';

import { Place } from '@/app/_components/place';
import { getPlaces } from '@/app/_components/place/action';

export default async function PopularPlacesPage() {
  const memberJson = await authenticateUser();
  const memberInfo = myInfoSchema.safeParse(memberJson);

  const result = await getPlaces('popular', 'more');

  if (result.status === 'failed') {
    console.log(result.message);

    return <main>Failed</main>;
  }

  const places = result.data;

  return (
    <main className="px-4 max-w-full flex flex-col">
      <h1 className="pl-4 pt-4 text-3xl font-medium">인기 장소</h1>
      <div className="flex flex-col gap-4 relative">
        {places.map((place, index) => (
          <Place
            key={place.id}
            data={place}
            order={index}
            isLoggedIn={memberInfo.success}
          />
        ))}
      </div>
    </main>
  );
}
