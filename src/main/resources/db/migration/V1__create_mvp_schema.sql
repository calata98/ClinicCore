create table clinics (
    id uuid primary key,
    name varchar(160) not null,
    legal_name varchar(200),
    phone varchar(40),
    email varchar(320),
    active boolean not null default true,
    created_at timestamp with time zone not null
);

create table user_accounts (
    id uuid primary key,
    clinic_id uuid references clinics(id),
    email varchar(320) not null,
    password_hash varchar(255) not null,
    full_name varchar(180) not null,
    role varchar(40) not null,
    active boolean not null default true,
    created_at timestamp with time zone not null,
    last_login_at timestamp with time zone,
    constraint uq_user_accounts_email unique (email)
);

create table refresh_tokens (
    id uuid primary key,
    user_id uuid not null references user_accounts(id),
    token_hash varchar(128) not null,
    expires_at timestamp with time zone not null,
    revoked_at timestamp with time zone,
    created_at timestamp with time zone not null,
    constraint uq_refresh_tokens_hash unique (token_hash)
);

create table professionals (
    id uuid primary key,
    clinic_id uuid not null references clinics(id),
    user_id uuid references user_accounts(id),
    first_name varchar(120) not null,
    last_name varchar(160) not null,
    email varchar(320),
    phone varchar(40),
    color varchar(20),
    active boolean not null default true,
    created_at timestamp with time zone not null
);

create table rooms (
    id uuid primary key,
    clinic_id uuid not null references clinics(id),
    name varchar(120) not null,
    active boolean not null default true,
    created_at timestamp with time zone not null,
    constraint uq_rooms_clinic_name unique (clinic_id, name)
);

create table clinic_services (
    id uuid primary key,
    clinic_id uuid not null references clinics(id),
    name varchar(160) not null,
    duration_minutes integer not null,
    price numeric(12,2),
    active boolean not null default true,
    created_at timestamp with time zone not null,
    constraint uq_services_clinic_name unique (clinic_id, name)
);

create table patients (
    id uuid primary key,
    clinic_id uuid not null references clinics(id),
    first_name varchar(120) not null,
    last_name varchar(160) not null,
    phone varchar(40),
    email varchar(320),
    birth_date date,
    administrative_notes varchar(2000),
    consent_accepted boolean not null default false,
    active boolean not null default true,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table appointments (
    id uuid primary key,
    clinic_id uuid not null references clinics(id),
    patient_id uuid not null references patients(id),
    professional_id uuid not null references professionals(id),
    room_id uuid not null references rooms(id),
    service_id uuid not null references clinic_services(id),
    start_at timestamp with time zone not null,
    end_at timestamp with time zone not null,
    status varchar(40) not null,
    reason varchar(500),
    cancellation_reason varchar(500),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_appointments_professional_time on appointments (clinic_id, professional_id, start_at, end_at);
create index idx_appointments_room_time on appointments (clinic_id, room_id, start_at, end_at);
create index idx_appointments_patient on appointments (clinic_id, patient_id);

create table treatment_episodes (
    id uuid primary key,
    clinic_id uuid not null references clinics(id),
    patient_id uuid not null references patients(id),
    responsible_professional_id uuid not null references professionals(id),
    title varchar(180) not null,
    start_date date not null,
    end_date date,
    status varchar(40) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table session_notes (
    id uuid primary key,
    clinic_id uuid not null references clinics(id),
    episode_id uuid not null references treatment_episodes(id),
    appointment_id uuid references appointments(id),
    professional_id uuid not null references professionals(id),
    session_date date not null,
    pain_level integer,
    treated_area varchar(240),
    techniques_applied varchar(1000),
    observations varchar(4000),
    next_recommendation varchar(1000),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_session_notes_episode on session_notes (clinic_id, episode_id, session_date);

create table treatment_packages (
    id uuid primary key,
    clinic_id uuid not null references clinics(id),
    name varchar(160) not null,
    total_sessions integer not null,
    price numeric(12,2),
    active boolean not null default true,
    created_at timestamp with time zone not null
);

create table patient_packages (
    id uuid primary key,
    clinic_id uuid not null references clinics(id),
    patient_id uuid not null references patients(id),
    package_id uuid not null references treatment_packages(id),
    total_sessions integer not null,
    remaining_sessions integer not null,
    paid_amount numeric(12,2),
    payment_method varchar(80),
    purchased_at timestamp with time zone not null,
    active boolean not null default true,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table exercises (
    id uuid primary key,
    clinic_id uuid not null references clinics(id),
    name varchar(180) not null,
    description varchar(2000),
    video_url varchar(1000),
    image_url varchar(1000),
    active boolean not null default true,
    created_at timestamp with time zone not null
);

create table exercise_plans (
    id uuid primary key,
    clinic_id uuid not null references clinics(id),
    patient_id uuid not null references patients(id),
    professional_id uuid not null references professionals(id),
    title varchar(180) not null,
    notes varchar(2000),
    starts_on date not null,
    active boolean not null default true,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table exercise_plan_items (
    id uuid primary key,
    plan_id uuid not null references exercise_plans(id),
    exercise_id uuid not null references exercises(id),
    series integer,
    repetitions integer,
    frequency varchar(160),
    notes varchar(1000),
    created_at timestamp with time zone not null
);

create table audit_logs (
    id uuid primary key,
    clinic_id uuid references clinics(id),
    actor_user_id uuid references user_accounts(id),
    action varchar(80) not null,
    resource_type varchar(120) not null,
    resource_id uuid,
    details varchar(2000),
    occurred_at timestamp with time zone not null,
    created_at timestamp with time zone not null
);

create index idx_audit_logs_clinic_time on audit_logs (clinic_id, occurred_at);
