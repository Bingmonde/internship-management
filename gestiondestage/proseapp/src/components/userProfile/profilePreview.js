import {getUserInfo} from "../../utils/userInfo";
import {telephoneFormat} from "../../api/utils";
import {useTranslation} from "react-i18next";
import {Permission} from "../../constants";


const ProfilePreview = ({ permission, profile }) => {
    const userType = getUserInfo().userType
    const { t } = useTranslation();
    const lang = getUserInfo().lang;

    console.log("profilee")

    console.log('profile', profile)
    return(
        <div className="profile">
            <div className="profile-section ">
                <h3 className='profile-section-title'>{t('profile.basicInfo')}</h3>
                {profile.prenom &&
                    <div className='flex flex-row'>
                        <p className='profile-label'>{t('profile.name')}:</p>
                        <p>{profile.prenom} {profile.nom}</p>
                    </div>
                }

                {profile.nomCompagnie &&
                    <div className='flex flex-row'>
                        <p className='profile-label'>{t('profile.companyName')}:</p>
                        <p>{profile.nomCompagnie}</p>
                    </div>
                }
                { (userType.toString().toLowerCase() === 'employeur' ||
                    userType.toString().toLowerCase() === 'teacher' ||
                    userType.toString().toLowerCase() === 'projet_manager' || permission == Permission.Full)
                    && profile.contactPerson &&
                    <div className='flex flex-row'>
                        <p className='profile-label'>{t('profile.contactPerson')}:</p>
                        <p>{profile.contactPerson}</p>
                    </div>
                }

                {profile.discipline &&
                    <div className='flex flex-row'>
                        <p className='profile-label'>{t('profile.discipline')}:</p>
                        <p>{profile.discipline[lang]}</p>
                    </div>
                }
            </div>

            <div className="profile-section">
                <h3 className='profile-section-title'>{t('profile.contactInfo')}</h3>
                <div className='flex flex-row'>
                    <p className='profile-label'>{t('profile.email')}:</p>
                    <p><a href={'mailto:' + profile.courriel} className="email">{profile.courriel}</a></p>
                </div>
                <div className='flex flex-row'>
                    <p className='profile-label'>{t('profile.telephone')}:</p>
                    <p>{telephoneFormat(profile.telephone)}</p>
                </div>
                {profile.fax &&
                    <div className='flex flex-row'>
                        <p className='profile-label'>{t('profile.fax')}:</p>
                        <p>{telephoneFormat(profile.fax)}</p>
                    </div>}
            </div>

            {(profile.nomCompagnie || permission == Permission.Full) &&
                <div className="profile-section">
                    <h3 className='profile-section-title'>{t('profile.addressInfo')}</h3>
                    <div className='flex flex-row'>
                        <p className='profile-label'>{t('profile.address')}:</p>
                        <p>{profile.adresse}</p>
                    </div>
                    {profile.city &&
                        <>
                            <div className='flex flex-row'>
                                <p className='profile-label'>{t('profile.city')}:</p>
                                <p>{profile.city}</p>
                            </div>
                            <div className='flex flex-row'>
                                <p className='profile-label'>{t('profile.postalCode')}:</p>
                                <p>{profile.postalCode}</p>
                            </div>
                        </>}
                </div>
            }


        </div>
    )
}

export default ProfilePreview;