'use client';
import React, { useEffect, useState } from 'react';
import Bell from '/public/icons/bell.svg';
import { useRouter } from 'next/navigation';
//import { EventSourcePolyfill, NativeEventSource } from 'event-source-polyfill';
export default function NotificationIcon() {
  const [newData, setNewData] = useState(false);
  useEffect(() => {
    const eventSource = new EventSource(
      `http://localhost:3000/endpoints/api/notification`,
      // `http://3.34.236.45:8080/api/v1/notifications/stream`,
      {
        withCredentials: true,
      },
    );
    console.log('eve', eventSource);
    console.log('ready', eventSource.readyState);

    eventSource.onopen = (e) => {
      console.log(e);
      console.log('opened');
    };
    eventSource.onerror = (e) => {
      console.log(e);
    };

    eventSource.onmessage = (e) => {
      console.log(e.data);
    };

    eventSource.addEventListener('notification', (e) => {
      console.log(e.data);
    });

    return () => eventSource.close();
  }, []);

  const { push } = useRouter();
  return (
    <div className="relative flex items-center">
      <div className="p-[6px] rounded-full bg-red-500 absolute top-[2px] right-[6px]"></div>
      <button onClick={() => push('/notification')}>
        <Bell className="w-10 h-10" />
      </button>
    </div>
  );
}
