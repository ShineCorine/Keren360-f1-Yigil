'use client';

import { useRef, useState } from 'react';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';

import SearchIcon from '/public/icons/search.svg';
import XMarkIcon from '/public/icons/x-mark.svg';

export default function SearchBar({
  cancellable,
  addHistory,
  search,
}: {
  cancellable?: boolean;
  addHistory: (result: string) => void;
  search: (keyword: string) => Promise<void>;
}) {
  const inputRef = useRef<HTMLInputElement>(null);

  const searchParams = useSearchParams();
  const pathname = usePathname();
  const { back, push, replace } = useRouter();

  const initialSearchValue = searchParams.get('keyword') || '';

  const [searchValue, setSearchValue] = useState(initialSearchValue);

  function handleChange(term: string) {
    setSearchValue(term);

    const params = new URLSearchParams(searchParams);

    if (term) {
      params.set('keyword', term);
    } else {
      params.delete('keyword');
    }

    replace(`${pathname}?${params.toString()}`);
  }

  // 리팩토링 필요
  async function handleSearch() {
    addHistory(searchValue);
    const params = new URLSearchParams(searchParams);

    if (pathname === '/search') {
      push(`${pathname}/result?${params.toString()}`);
    } else {
      console.log('yay');
      search(searchValue);
    }
  }

  function handleErase() {
    setSearchValue('');
    search('');
    inputRef.current?.focus();
    replace(`${pathname}`);
  }

  return (
    <div className="flex gap-4 px-6">
      <div className="w-full px-4 py-2 bg-[#e5e7eb] shadow-xl rounded-full flex gap-4 items-center">
        <input
          className="w-full text-lg bg-transparent outline-none focus:border-b-2 focus:border-black"
          ref={inputRef}
          type="text"
          placeholder="검색어를 입력하세요."
          value={searchValue}
          onChange={(event) => handleChange(event.target.value)}
        />
        {searchParams.size !== 0 && (
          <button
            className="p-1 bg-gray-400 rounded-full flex justify-center items-center"
            onClick={handleErase}
          >
            <XMarkIcon className="w-3 h-3 stroke-white stroke-[1.25]" />
          </button>
        )}
        {/*  */}
        <button onClick={handleSearch}>
          <SearchIcon className="w-6 h-6 shrink-0" />
        </button>
      </div>
      {cancellable && (
        <button className="shrink-0" onClick={() => back()}>
          취소
        </button>
      )}
    </div>
  );
}
