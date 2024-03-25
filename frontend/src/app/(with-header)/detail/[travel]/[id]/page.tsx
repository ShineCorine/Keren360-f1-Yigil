import CourseDetail from '@/app/_components/mypage/course/CourseDetail';
import { authenticateUser } from '@/app/_components/mypage/hooks/authenticateUser';
import {
  getCourseDetail,
  getSpotDetail,
} from '@/app/_components/mypage/hooks/myPageActions';
import SpotDetail from '@/app/_components/mypage/spot/SpotDetail';
import { myInfoSchema } from '@/types/response';
import React from 'react';

export default async function SpotDetailPage({
  params,
}: {
  params: { id: number; travel: string };
}) {
  const user = await authenticateUser();
  const parsedUser = myInfoSchema.safeParse(user);
  const isLoggedIn = parsedUser.success;
  if (params.travel === 'spot') {
    const spotDetail = await getSpotDetail(params.id);

    if (!spotDetail.success)
      return (
        <div className="w-full h-full flex flex-col break-words justify-center items-center text-3xl text-center text-main">
          장소 상세 정보를 불러오는데 실패했습니다. <hr /> 다시 시도해주세요.
        </div>
      );

    return (
      <SpotDetail
        spotDetail={spotDetail.data}
        spotId={params.id}
        isLoggedIn={isLoggedIn}
      />
    );
  } else {
    const courseDetail = await getCourseDetail(params.id);
    if (!courseDetail.success)
      return (
        <div className="w-full h-full flex flex-col break-words justify-center items-center text-3xl text-center text-main">
          코스 상세 정보를 불러오는데 실패했습니다. <hr /> 다시 시도해주세요.
        </div>
      );

    return (
      <CourseDetail
        courseDetail={courseDetail.data}
        courseId={params.id}
        isLoggedIn={isLoggedIn}
      />
    );
  }
}