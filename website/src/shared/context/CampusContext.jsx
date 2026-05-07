import { createContext, useContext, useEffect, useState } from 'react';
import campusService from '../services/campusService';

const CampusContext = createContext();

export const CampusProvider = ({ children }) => {
    const [campuses, setCampuses] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        campusService.getAllCampuses().then((result) => {
            if (result.success) setCampuses(result.data);
            setLoading(false);
        });
    }, []);

    const refreshCampuses = async () => {
        const result = await campusService.getAllCampuses();
        if (result.success) setCampuses(result.data);
    };

    return (
        <CampusContext.Provider value={{ campuses, campusesLoading: loading, refreshCampuses }}>
            {children}
        </CampusContext.Provider>
    );
};

export const useCampuses = () => {
    const context = useContext(CampusContext);
    if (!context) {
        throw new Error('useCampuses must be used within a CampusProvider');
    }
    return context;
};
