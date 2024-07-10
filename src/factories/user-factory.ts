import User from '../models/user.js';

export default class UserFactory {
    /**
     * @return {import('../models/user.mjs')}
     */
    create() {
        return new User();
    }
}
