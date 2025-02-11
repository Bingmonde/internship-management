import React, {useEffect, useState} from 'react';

// this component has terrible UX but WHO CARES
const PaginationComponent = ({ totalPages, paginate, resetCurrentPage}) => {

    useEffect(() => {
        if (!resetCurrentPage) return
        setCurrentPage(1)
    }, [resetCurrentPage]);

    const [currentPage, setCurrentPage] = useState(1);

    const handleClick = (pageNumber) => {
        setCurrentPage(pageNumber);
        paginate(pageNumber);
    };

    const renderPageNumbers = () => {
        const pageNumbers = [];

        if (!totalPages) return pageNumbers;

        if (totalPages <= 1) return pageNumbers;

        const buttonClass = "inline-block mx-1 px-3 py-1 rounded cursor-pointer bg-darkpurple-option text-white text-sm";
        const disabledButtonClass = "inline-block mx-1 px-3 py-1 border border-gray-300 rounded bg-gray-300 text-gray-600 text-sm";

        pageNumbers.push(
            <li key="prev" className={`page-item ${currentPage === 1 ? 'disabled' : ''}`}>
                <button onClick={() => handleClick(currentPage - 1)} className={currentPage === 1 ? disabledButtonClass : buttonClass} disabled={currentPage === 1}>
                    &lt;
                </button>
            </li>
        );

        if (currentPage > 2) {
            pageNumbers.push(
                <li key={1} className="page-item">
                    <button onClick={() => handleClick(1)} className={buttonClass}>
                        1
                    </button>
                </li>
            );
            if (currentPage > 3) {
                pageNumbers.push(
                    <li key="dots1" className="page-item disabled">
                        <span className={buttonClass}>..</span>
                    </li>
                );
            }
        }

        for (let i = Math.max(1, currentPage - 1); i <= Math.min(totalPages, currentPage + 1); i++) {
            pageNumbers.push(
                <li key={i} className={`page-item ${currentPage === i ? 'active' : ''}`}>
                    <button onClick={() => handleClick(i)} className={currentPage === i ? disabledButtonClass : buttonClass} disabled={currentPage === i}>
                        {i}
                    </button>
                </li>
            );
        }

        if (currentPage < totalPages - 1) {
            if (currentPage < totalPages - 2) {
                pageNumbers.push(
                    <li key="dots2" className="page-item disabled">
                        <span className={buttonClass}>..</span>
                    </li>
                );
            }
            pageNumbers.push(
                <li key={totalPages} className="page-item">
                    <button onClick={() => handleClick(totalPages)} className={buttonClass}>
                        {totalPages}
                    </button>
                </li>
            );
        }

        pageNumbers.push(
            <li key="next" className={`page-item ${currentPage === totalPages ? 'disabled' : ''}`}>
                <button onClick={() => handleClick(currentPage + 1)} className={currentPage === totalPages ? disabledButtonClass : buttonClass} disabled={currentPage === totalPages}>
                    &gt;
                </button>
            </li>
        );

        return pageNumbers;
    };

    return (
        <nav className="m-3">
            <ul className="pagination list-none p-0 flex">
                {renderPageNumbers()}
            </ul>
        </nav>
    );
};

export default PaginationComponent;