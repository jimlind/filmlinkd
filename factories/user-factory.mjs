import User from '../models/user.mjs';

export default class UserFactory {
    /**
     * @return {import('../models/user.mjs')}
     */
    create() {
        return new User();
    }
}
