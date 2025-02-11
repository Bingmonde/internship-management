import React, { useState, useEffect } from 'react';

const SearchComponent = ({ placeholderText, stoppedTyping}) => {
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            stoppedTyping(searchTerm);
        }, 500); // 500ms debounce delay

        return () => clearTimeout(delayDebounceFn);
    }, [searchTerm]);

    return (
        <div className="w-1/2 mx-auto">
            <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder={placeholderText}
                className="w-full p-2 mb-5 border border-gray-300 rounded"
            />
        </div>
    );
};

export default SearchComponent;