import {useEffect, useState} from "react";
import {getUserInfo} from "../../utils/userInfo";
import {useTranslation} from "react-i18next";
import {telephoneFormat} from "../../api/utils";
import ProfilePreview from "./profilePreview";
import {Permission} from "../../constants";
import EditProfileModal from "../EditProfileModal";


const MyProfile = ({updateUsername}) => {
    const [user, setUser] = useState(null);
    const [errorMessage, setErrorMessage] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);

    const { t } = useTranslation();
    const lang = getUserInfo().lang;

    useEffect(() => {
        const getUserProfile = async () => {
            const res = await fetch('http://localhost:8080/userinfo/myProfile', {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                },
            })
            if (res.ok){
                console.log('user profile',res)
                const data = await res.json();
                console.log('user profile',data.value)
                setUser(data.value);
            } else {
                const err = await res.json();
                console.log(err)
                console.log(err["exception"])
                console.error('Error while fetching user profile');
            }
        }
        getUserProfile();
    }, []);


    // Open modal
    const openModal = () => {
        setIsModalOpen(true);
    };

    // Close modal
    const closeModal = () => {
        setIsModalOpen(false);
    };

    // Update user profile
    const handleUpdateProfile = (updatedProfile) => {
        setUser((prevUser) => ({
            ...prevUser,
            user: updatedProfile, // Update 'user' property
        }));
        closeModal();
    };


    return (
        <div className='profile'>
            <h2>{t('dashboardMenu.monProfile')}</h2>
            {errorMessage && <p className="text-red-500 mb-4">{errorMessage}</p>}
            {user == null && <p>{t('Loading...')}</p>}
            {user &&
                <>
                    <h3 className="text-darkpurple text-xl">{t('profile.accountType')}: {user && t('userTypes.' + user.role.toString().toLowerCase())}</h3>
                    <ProfilePreview permission={Permission.Full} profile={user.user} />

                    {/* Edit button */}
                    <button
                        onClick={openModal}
                        className="btn-neutral w-1/2 mb-5"
                    >
                        {t("profile.editProfile")}
                    </button>

                    {/* Edit profile modal */}
                    {isModalOpen && (
                        <EditProfileModal
                            profile={user.user}
                            onClose={closeModal}
                            onUpdateProfile={handleUpdateProfile}
                            permission={Permission.Full}
                            updateUsername={updateUsername}
                        />
                    )}
                </>

            }


        </div>
    );

}


export default MyProfile;