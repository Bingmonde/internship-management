import {render, screen} from "@testing-library/react";
import ProfilePreview from "./profilePreview";
import {Permission} from "../../constants";
import {getUserInfo, setUserInfo} from "../../utils/userInfo";
import  "../../utils/i18n/index";

beforeAll(() => {
    localStorage.setItem("lang", "en")
})

const mockProfileStudent = {
    id: 1,
    nom: "Doe",
    prenom: "John",
    courriel: "test@email.com",
    adresse: "1234 rue de la rue",
    telephone: "1234567890",
    discipline: {
        id: "informatique",en:"Computer Science",fr:"Informatique"
    }
}

const mockProfileEmployer = {
    id: 2,
    nomCompagnie: "Hydro Quebec",
    contactPerson: "Jane Doe",
    courriel: "emp@email.com",
    adresse: "1234 rue de la rue",
    telephone: "1234567890",
    fax: "1234567890",
    city: "Montreal",
    postalCode: "H3H 2H2",
}

const mockProfileTeacher = {
    id: 3,
    nom: "Smith",
    prenom: "Jack",
    courriel: "prof@email.com",
    adresse: "1234 rue de la rue",
    telephone: "1234567890",
    discipline: {
        id: "informatique",en:"Computer Science",fr:"Informatique"
    }
}

const mockProfileProjectManager = {
    id: 4,
    nom: "Smith",
    prenom: "Jane",
    courriel: "gs@email.com",
    adresse: "1234 rue de la rue",
    telephone: "1234567890",

}

describe('Test l\'aperçu du profile del\'utilisateur', () => {

    it('should display a student profile',  () => {
        setUserInfo("STUDENT","xdafdsfs8FSDFSDxx", "John Doe", )
        console.log("mockProfileStudent", mockProfileStudent)
        console.log('langue', getUserInfo().lang)
        render(<ProfilePreview permission={Permission.Full} profile={mockProfileStudent} />);
        const nameElement = screen.getByText(/John Doe/i);
        const emailElement = screen.getByText(/test@email.com/i);

        const telephoneElement = screen.getByText(/\(123\) 456-7890/);
        const addressElement = screen.getByText(/1234 rue de la rue/i);
        const disciplineElement = screen.getByText(/Computer Science/i);
        expect(nameElement).toBeInTheDocument();
        expect(emailElement).toBeInTheDocument();

        expect(telephoneElement).toBeInTheDocument();
        expect(addressElement).toBeInTheDocument();
        expect(disciplineElement).toBeInTheDocument();

        }
    );

    it('should display a employer profile',  () => {
            setUserInfo("EMPLOYEUR","xdafdsfs8FSDFSDxx", "Hydro Quebec", )
            render(<ProfilePreview permission={Permission.Full} profile={mockProfileEmployer} />);
            const companyNameElement = screen.getByText(/Hydro Quebec/i);
            const nameElement = screen.getByText(/Jane Doe/i);
            const emailElement = screen.getByText(/emp@email.com/i);
            const telephoneElement = screen.getAllByText(/\(123\) 456-7890/);
            const addressElement = screen.getByText(/1234 rue de la rue/i);
            const cityElement = screen.getByText(/Montreal/i);
            const postalCodeElement = screen.getByText(/H3H 2H2/i);
            expect(companyNameElement).toBeInTheDocument();
            expect(nameElement).toBeInTheDocument();
            expect(emailElement).toBeInTheDocument();
            expect(telephoneElement[0]).toBeInTheDocument()
            expect(telephoneElement[1]).toBeInTheDocument();
            expect(addressElement).toBeInTheDocument();
            expect(cityElement).toBeInTheDocument();
            expect(postalCodeElement).toBeInTheDocument();
        }
    );

    it('should display a prof profile',  () => {
            setUserInfo("TEACHER","xdafdsfs8FSDFSDxx", "Jack Smith", )
            render(<ProfilePreview permission={Permission.Full} profile={mockProfileTeacher} />);
            const nameElement = screen.getByText(/Jack Smith/i);
            const emailElement = screen.getByText(/prof@email.com/i);

            const telephoneElement = screen.getByText(/\(123\) 456-7890/);
            const addressElement = screen.getByText(/1234 rue de la rue/i);
            const disciplineElement = screen.getByText(/Computer Science/i);
            expect(nameElement).toBeInTheDocument();
            expect(emailElement).toBeInTheDocument();
            expect(telephoneElement).toBeInTheDocument();
            expect(addressElement).toBeInTheDocument();
            expect(disciplineElement).toBeInTheDocument();

        }
    );

    it('should display a project manager profile',  () => {
            setUserInfo("PROJECT_MANAGER","xdafdsfs8FSDFSDxx", "Jane Smith", )
            render(<ProfilePreview permission={Permission.Full} profile={mockProfileProjectManager} />);
            const nameElement = screen.getByText(/Jane Smith/i);
            const emailElement = screen.getByText(/gs@email.com/i);
            const telephoneElement = screen.getByText(/\(123\) 456-7890/);
            const addressElement = screen.getByText(/1234 rue de la rue/i);
            expect(nameElement).toBeInTheDocument();
            expect(emailElement).toBeInTheDocument();
            expect(telephoneElement).toBeInTheDocument();
            expect(addressElement).toBeInTheDocument();
        }
    );

    it('should display a student intern except address when employer consult a intern',  () => {
            setUserInfo("EMPLOYEUR","xdafdsfs8FSDFSDxx", "Hydro Quebec", )
            render(<ProfilePreview permission={Permission.Limited} profile={mockProfileStudent} />);
            const nameElement = screen.getByText(/John Doe/i);
            const emailElement = screen.getByText(/test@email.com/i);
            const telephoneElement = screen.getByText(/\(123\) 456-7890/);
            const addressElement = screen.queryByText(/1234 rue de la rue/i);
            const disciplineElement = screen.getByText(/Computer Science/i);
            expect(nameElement).toBeInTheDocument();
            expect(emailElement).toBeInTheDocument();
            expect(telephoneElement).toBeInTheDocument();
            expect(addressElement).toBeNull();
            expect(disciplineElement).toBeInTheDocument();
        }
    );

    it('should display a employer profile when student consults an employer with limited permission ',  () => {
        setUserInfo("STUDENT","xdafdsfs8FSDFSDxx", "John Doe", )
            render(<ProfilePreview permission={Permission.Limited} profile={mockProfileEmployer} />);
            const companyNameElement = screen.getByText(/Hydro Quebec/i);
            const nameElement = screen.queryByText(/Jane Doe/i);
            const emailElement = screen.getByText(/emp@email.com/i);
            const telephoneElement = screen.getAllByText(/\(123\) 456-7890/);
            const addressElement = screen.getByText(/1234 rue de la rue/i);
            const cityElement = screen.getByText(/Montreal/i);
            const postalCodeElement = screen.getByText(/H3H 2H2/i);
            expect(companyNameElement).toBeInTheDocument();
            expect(nameElement).toBeNull();
            expect(emailElement).toBeInTheDocument();
            expect(telephoneElement[0]).toBeInTheDocument()
            expect(telephoneElement[1]).toBeInTheDocument();
            expect(addressElement).toBeInTheDocument();
            expect(cityElement).toBeInTheDocument();
            expect(postalCodeElement).toBeInTheDocument();
        }
    );


    it('should display a employer profile when student consults an employer with full permission ',  () => {
            setUserInfo("STUDENT","xdafdsfs8FSDFSDxx", "John Doe", )
            render(<ProfilePreview permission={Permission.Full} profile={mockProfileEmployer} />);
            const companyNameElement = screen.getByText(/Hydro Quebec/i);
            const nameElement = screen.getByText(/Jane Doe/i);
            const emailElement = screen.getByText(/emp@email.com/i);
            const telephoneElement = screen.getAllByText(/\(123\) 456-7890/);
            const addressElement = screen.getByText(/1234 rue de la rue/i);
            const cityElement = screen.getByText(/Montreal/i);
            const postalCodeElement = screen.getByText(/H3H 2H2/i);
            expect(companyNameElement).toBeInTheDocument();
            expect(nameElement).toBeInTheDocument();
            expect(emailElement).toBeInTheDocument();
            expect(telephoneElement[0]).toBeInTheDocument()
            expect(telephoneElement[1]).toBeInTheDocument();
            expect(addressElement).toBeInTheDocument();
            expect(cityElement).toBeInTheDocument();
            expect(postalCodeElement).toBeInTheDocument();
        }
    );

    it('should display a employer profile when professor consults an employer with full permission ',  () => {
            setUserInfo("TEACHER","xdafdsfs8FSDFSDxx", "Jack Smith", )
            render(<ProfilePreview permission={Permission.Full} profile={mockProfileEmployer} />);
            const companyNameElement = screen.getByText(/Hydro Quebec/i);
            const nameElement = screen.getByText(/Jane Doe/i);
            const emailElement = screen.getByText(/emp@email.com/i);
            const telephoneElement = screen.getAllByText(/\(123\) 456-7890/);
            const addressElement = screen.getByText(/1234 rue de la rue/i);
            const cityElement = screen.getByText(/Montreal/i);
            const postalCodeElement = screen.getByText(/H3H 2H2/i);
            expect(companyNameElement).toBeInTheDocument();
            expect(nameElement).toBeInTheDocument();
            expect(emailElement).toBeInTheDocument();
            expect(telephoneElement[0]).toBeInTheDocument()
            expect(telephoneElement[1]).toBeInTheDocument();
            expect(addressElement).toBeInTheDocument();
            expect(cityElement).toBeInTheDocument();
            expect(postalCodeElement).toBeInTheDocument();
        }
    );

    it('should display a professor profile when project manager consults a professor',  () => {
        setUserInfo("PROJECT_MANAGER","xdafdsfs8FSDFSDxx", "Jane Smith", )
        render(<ProfilePreview permission={Permission.Full} profile={mockProfileTeacher} />);
        const nameElement = screen.getByText(/Jack Smith/i);
        const emailElement = screen.getByText(/prof@email.com/i);

        const telephoneElement = screen.getByText(/\(123\) 456-7890/);
        const addressElement = screen.getByText(/1234 rue de la rue/i);
        const disciplineElement = screen.getByText(/Computer Science/i);
        expect(nameElement).toBeInTheDocument();
        expect(emailElement).toBeInTheDocument();
        expect(telephoneElement).toBeInTheDocument();
        expect(addressElement).toBeInTheDocument();
        expect(disciplineElement).toBeInTheDocument();
        }
    );


})