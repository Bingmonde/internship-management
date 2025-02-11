import React, {forwardRef, useEffect, useRef} from 'react';
import SignaturePad from "react-signature-pad-wrapper";

const signaturePadWrapper = forwardRef(({isEmptyState: setEmptyState}, ref) => {

    useEffect(() => {
        const element = ref.current.signaturePad;
        console.log(element)

        // Ensure the element exists before adding an event listener
        if (element) {
            const eventHandler = (event) => {
                if (ref.current.isEmpty) {
                    setEmptyState(true)
                } else {
                    setEmptyState(false)
                }
            };

            // Replace 'customEvent' with the event you want to listen to
            element.addEventListener('endStroke', eventHandler);

            // Cleanup function to remove the event listener when the component unmounts
            return () => {
                element.removeEventListener('endStroke', eventHandler);
            };
        }
    }, []);

    return (
        <SignaturePad ref={ref} redrawOnResize={true} height={100} width={500}
                      options={{backgroundColor: "rgb(255,255,255)"}} canvasProps={{className: "w-[500px] h-[100px]"}}></SignaturePad>
    );
});

export default signaturePadWrapper;