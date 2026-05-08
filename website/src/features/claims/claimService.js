import api from '../../shared/services/api';

const claimService = {
    async submitClaim(claimData) {
        try {
            const response = await api.post('/claims', claimData);
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || 'Failed to submit claim' };
        }
    },

    async getMyClaims(page = 0, size = 20) {
        try {
            const response = await api.get(`/claims/my?page=${page}&size=${size}`);
            return { success: true, data: response.data.content, totalPages: response.data.totalPages, totalElements: response.data.totalElements };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || 'Failed to fetch your claims' };
        }
    },

    async getIncomingClaims(page = 0, size = 20) {
        try {
            const response = await api.get(`/claims/incoming?page=${page}&size=${size}`);
            return { success: true, data: response.data.content, totalPages: response.data.totalPages, totalElements: response.data.totalElements };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || 'Failed to fetch incoming claims' };
        }
    },

    async getClaimsForItem(itemId, page = 0, size = 20) {
        try {
            const response = await api.get(`/claims/item/${itemId}?page=${page}&size=${size}`);
            return { success: true, data: response.data.content, totalPages: response.data.totalPages, totalElements: response.data.totalElements };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || 'Failed to fetch claims for item' };
        }
    },

    async getClaimById(claimId) {
        try {
            const response = await api.get(`/claims/${claimId}`);
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || 'Failed to fetch claim' };
        }
    },

    async acceptClaim(claimId) {
        try {
            const response = await api.put(`/claims/${claimId}/accept`);
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || 'Failed to accept claim' };
        }
    },

    async rejectClaim(claimId) {
        try {
            const response = await api.put(`/claims/${claimId}/reject`);
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || 'Failed to reject claim' };
        }
    },

    async cancelClaim(claimId) {
        try {
            const response = await api.put(`/claims/${claimId}/cancel`);
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || 'Failed to cancel claim' };
        }
    },

    async markItemReturned(claimId) {
        try {
            const response = await api.put(`/claims/${claimId}/mark-returned`);
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || 'Failed to mark item as returned' };
        }
    },

    async confirmItemReceived(claimId) {
        try {
            const response = await api.put(`/claims/${claimId}/confirm-received`);
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || 'Failed to confirm item received' };
        }
    },

    async disputeHandover(claimId) {
        try {
            const response = await api.put(`/claims/${claimId}/dispute-handover`);
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || 'Failed to dispute handover' };
        }
    },
};

export default claimService;
