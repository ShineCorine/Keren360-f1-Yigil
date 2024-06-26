'use client';
import React, { useState } from 'react';
import BackButton from '../../place/BackButton';
import { TMyPageSpotDetail } from '@/types/myPageResponse';
import StarIcon from '/public/icons/star.svg';
import useEmblaCarousel from 'embla-carousel-react';
import Image from 'next/image';
import { TImageData } from '../../images/ImageHandler';
import { patchSpotDetail } from '../hooks/myPageActions';
import Dialog from '../../ui/dialog/Dialog';
import ToastMsg from '../../ui/toast/ToastMsg';
import * as Common from '../detailModify';
import IconWithCounts from '../../IconWithCounts';

export interface TModifyDetail {
  id: number;
  rate: string;
  description: string;
  image_urls: { filename: string; uri: string }[];
}

export default function SpotDetail({
  spotDetail,
  spotId,
  isMySpot,
}: {
  spotDetail: TMyPageSpotDetail;
  spotId: number;
  isMySpot?: boolean;
}) {
  const {
    rate,
    place_name,
    place_address,
    map_static_image_file_url,
    image_urls,
    create_date,
    description,
  } = spotDetail;
  const [isModifyMode, setIsModifyMode] = useState(false);
  const [selectOption, setSelectOption] = useState(Number(rate).toFixed(1));

  const [modifyDetail, setModifyDetail] = useState<TModifyDetail>({
    id: spotId,
    rate,
    description,
    image_urls: image_urls.map((uri, idx) => ({
      filename: idx.toString(),
      uri,
    })),
  });

  const [isDialogOpened, setIsDialogOpened] = useState(false);
  const [errorText, setErrorText] = useState('');

  const [emblaRef] = useEmblaCarousel({
    loop: false,
    dragFree: true,
  });

  const setImages = (newImages: { type: 'new'; data: TImageData[] }) => {
    setModifyDetail({ ...modifyDetail, image_urls: newImages.data });
  };

  const onChangeDescription = (description: string) => {
    if (description.length > 30) {
      setErrorText('리뷰를 30자 이내로 입력해야 합니다.');
      setTimeout(() => {
        setErrorText('');
      }, 2000);
      return;
    } else {
      setModifyDetail((prev) => ({ ...prev, description }));
      setErrorText('');
    }
  };

  const onChangeSelectOption = (option: string) => {
    setSelectOption(option);
  };

  const onClickComplete = async () => {
    if (!modifyDetail.description) {
      setErrorText('리뷰를 1자이상 입력해야 합니다.');
      setTimeout(() => {
        setErrorText('');
      }, 2000);
      setIsDialogOpened(false);
      return;
    }

    setErrorText('');
    const res = await patchSpotDetail(spotId, modifyDetail);

    if (res.status === 'failed') {
      setErrorText(res.message);
      setTimeout(() => {
        setErrorText('');
      }, 2000);
    }

    setIsDialogOpened(false);
    setIsModifyMode(false);
  };

  const closeModal = () => {
    setIsDialogOpened(false);
  };
  return (
    <div className="mx-4 grow">
      <nav className="relative py-4 flex justify-between items-center">
        <BackButton />
        <span className="text-2xl font-light">
          {isModifyMode ? '기록 수정' : '장소 기록'}
        </span>
        {isMySpot ? (
          <button
            className={`${isModifyMode ? 'text-main' : 'text-gray-500'} py-2`}
            onClick={() => setIsModifyMode(true)}
          >
            {isModifyMode ? (
              <span onClick={() => setIsDialogOpened(true)}>완료</span>
            ) : (
              '수정'
            )}
          </button>
        ) : (
          <div></div>
        )}
      </nav>
      <section className="h-full p-2 flex flex-col gap-5 grow justify-between">
        <div className="flex justify-between items-center">
          <span className="text-2xl font-semibold">{place_name}</span>
          {isModifyMode ? (
            <Common.SelectContainer
              selectOption={selectOption}
              selectStyle="p-2"
              rate={rate}
              onChangeSelectOption={onChangeSelectOption}
            />
          ) : (
            <span className="text-gray-500 text-xl leading-6 flex items-center p-2 border-2 border-transparent">
              <IconWithCounts
                icon={<StarIcon className="w-6 h-6 fill-[#FAbb15]" />}
                count={parseInt(rate)}
                rating
              />
            </span>
          )}
        </div>
        <p className="text-gray-500">{place_address}</p>
        <div className="h-1/3 relative aspect-video">
          <Image
            className="rounded-md"
            src={map_static_image_file_url}
            alt="Spot map image"
            fill
          />
        </div>

        {isModifyMode ? (
          <Common.ModifyImage
            image_urls={modifyDetail.image_urls}
            setImages={setImages}
          />
        ) : (
          <div className="overflow-hidden" ref={emblaRef}>
            <div className="flex gap-2">
              {image_urls.map((image) => (
                <div
                  className="p-2 relative w-1/3 overflow-hidden aspect-square rounded-2xl border-2 border-gray-300 shrink-0"
                  key={image}
                >
                  <Image src={image} alt="Uploaded image" fill />
                </div>
              ))}
            </div>
          </div>
        )}

        <span className="self-end mt-[-8px] pr-4 text-gray-400">
          {create_date}
        </span>
        {isModifyMode ? (
          <Common.TextArea
            description={modifyDetail.description}
            onChangeHandler={onChangeDescription}
          />
        ) : (
          <div className="p-4 h-1/6 flex flex-col gap-2 bg-gray-100 rounded-xl text-lg justify-self-end border-2 border-transparent">
            {description}
          </div>
        )}
      </section>
      {isDialogOpened && (
        <Dialog
          text="수정을 완료하시겠습니까?"
          closeModal={closeModal}
          handleConfirm={async () => onClickComplete()}
          loadingText="수정중입니다."
        />
      )}
      {errorText && <ToastMsg title={errorText} timer={2000} id={Date.now()} />}
    </div>
  );
}
